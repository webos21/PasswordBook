import React, { Component } from 'react';
import {
  Button, Card, CardBody, CardHeader, Col, Row, Table,
  Form, Input, InputGroup, InputGroupAddon, InputGroupText,
} from 'reactstrap';
import Pager from '../../components/Pager/pager.js';
import PbFormAdd from './PbFormAdd.js';
import PbFormEdit from './PbFormEdit.js';
import PbFormDel from './PbFormDel.js';
import update from 'immutability-helper';

class PasswordBook extends Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.toggleFade = this.toggleFade.bind(this);

    this.dataChangedCallback = this.dataChangedCallback.bind(this);
    this.renderTableList = this.renderTableList.bind(this);

    this.handleViewAll = this.handleViewAll.bind(this);
    this.handleSearchGo = this.handleSearchGo.bind(this);
    this.handlePageChanged = this.handlePageChanged.bind(this);

    this.state = {
      dataSet: [],
      itemsPerPage: 4,
      totalPage: 0,
      currentPage: 0,
      visiblePages: 10,
      keyword: "",
      keywordError: "",
      collapse: true,
      fadeIn: true,
      timeout: 300
    };
  }

  toggle() {
    this.setState({ collapse: !this.state.collapse });
  }

  toggleFade() {
    this.setState((prevState) => { return { fadeIn: !prevState } });
  }

  dataChangedCallback(modifiedData) {
    console.log("PasswordBook::dataChangedCallback");
    if (modifiedData !== undefined && modifiedData !== null) {
      for (var i = 0; i < this.state.dataSet.length; i++) {
        if (this.state.dataSet[i].id === modifiedData.id) {
          console.log("\n\nbefore = " + JSON.stringify(this.state.dataSet[i]));
          var newDataSet = update(this.state.dataSet, { [i]: { $set: modifiedData } });
          console.log("\n\nafter = " + JSON.stringify(newDataSet[i]));
          this.setState({ dataSet: newDataSet });
          break;
        }
      }
    } else {
      this.requestFetch(this.state.keyword);
    }
  }

  requestFetch(query) {
    const parentState = this;
    const reqUri = 'http://localhost:28080/pwdata.do?q=' +
      (query === null || query === undefined ? '' : query);

    fetch(reqUri, {
      method: 'GET',
      headers: new Headers({
        'Authorization': 'Basic ' + btoa('username:password'),
      }),
      credentials: 'include',
    }).then(function (res) {
      if (!res.ok) {
        throw Error("서버응답 : " + res.statusText + "(" + res.status + ")");
      }
      return res.json();
    }).then(function (resJson) {
      console.log("PasswordBook::fetch => " + resJson.result);

      var dataLen = resJson.data.length;
      var calcPages = Math.ceil(dataLen / parentState.state.itemsPerPage);

      parentState.setState({
        dataSet: resJson.data,
        totalPage: calcPages,
        keywordError: '',
      });
    }).catch(function (error) {
      console.log("PasswordBook::fetch => " + error);
      parentState.setState({ keywordError: error.message })
    });
  }

  componentDidMount() {
    this.requestFetch();
  }

  handleViewAll() {
    this.setState({ keyword: "" });
    this.requestFetch("");
    document.getElementById("frmRefSearch").reset();
  }

  handlePageChanged(newPage) {
    this.setState({ currentPage: newPage });
  }

  handleSearchGo(event) {
    event.preventDefault();
    this.setState({ keyword: event.target.keyword.value });
    this.requestFetch(event.target.keyword.value);
  }

  renderTableList(dataArray) {
    if (dataArray.length === 0) {
      return (
        <tr key="row-nodata">
          <td colSpan="4" className="text-center align-middle" height="200">No Data</td>
        </tr>
      )
    } else {
      var firstIdx = this.state.currentPage * this.state.itemsPerPage;
      var lastIdx = this.state.currentPage * this.state.itemsPerPage + this.state.itemsPerPage;
      var tableData = dataArray.slice(firstIdx, lastIdx);

      return tableData.map((data, index) => {
        return (
          <tr key={'pbdata-' + data.id}>
            <td>{data.siteName}</td>
            <td>{data.siteType}</td>
            <td><a href={data.siteUrl} target="_blank" rel="noopener noreferrer">{data.siteUrl}</a></td>
            <td>{data.myId}</td>
            <td>
              <PbFormEdit dataFromParent={data} callbackFromParent={this.dataChangedCallback} />
              &nbsp;
              <PbFormDel dataFromParent={data} callbackFromParent={this.dataChangedCallback} />
            </td>
          </tr>
        )
      })
    }
  }

  render() {
    return (
      <div className="animated fadeIn">
        <Row>
          <Col>
            <Card>
              <CardHeader>
                <strong>Search</strong>
                <small> PasswordBook</small>
              </CardHeader>
              <CardBody>
                <Row>
                  <Col>
                    <Form onSubmit={this.handleSearchGo} id="frmRefSearch">
                      <InputGroup>
                        <InputGroupAddon addonType="prepend">
                          <InputGroupText>Keyword</InputGroupText>
                        </InputGroupAddon>
                        <Input type="text" name="keyword" placeholder="Enter the search keyword" />
                        <InputGroupAddon addonType="append">
                          <Button type="submit" color="primary">Search</Button>
                        </InputGroupAddon>
                        {this.state.keyword !== "" &&
                          <InputGroupAddon addonType="append">
                            <Button type="reset" color="success" onClick={this.handleViewAll}>전체보기</Button>
                          </InputGroupAddon>
                        }
                      </InputGroup>
                      <small id="keywordError" className="text-danger">{this.state.keywordError}</small>
                    </Form>
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row>

        <Row>
          <Col>
            <Card>
              <CardHeader>
                <i className="fa fa-align-justify"></i> Password List (Total : {this.state.dataSet.length})
                <PbFormAdd callbackFromParent={this.dataChangedCallback} />
              </CardHeader>
              <CardBody>
                <Table hover bordered striped responsive size="sm">
                  <thead>
                    <tr>
                      <th>이름</th>
                      <th>유형</th>
                      <th>항목 URL</th>
                      <th>ID</th>
                      <th>Edit</th>
                    </tr>
                  </thead>
                  <tbody>
                    {this.renderTableList(this.state.dataSet)}
                  </tbody>
                </Table>
                <Pager
                  total={this.state.totalPage}
                  current={this.state.currentPage}
                  visiblePages={this.state.visiblePages}
                  titles={{ first: 'First', last: 'Last' }}
                  onPageChanged={this.handlePageChanged}
                />
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>

    );
  }
}

export default PasswordBook;
