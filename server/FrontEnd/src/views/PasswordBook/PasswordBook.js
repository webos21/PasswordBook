import React, { Component } from 'react';
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Col,
  Form,
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
  Row,
  Table
} from 'reactstrap';
import Pager from '../../components/Pager/pager.js';
import PbFormAdd from './PbFormAdd.js';
import PbFormEdit from './PbFormEdit.js';
import PbFormDel from './PbFormDel.js';

class PasswordBook extends Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.toggleFade = this.toggleFade.bind(this);

    this.dataChangedCallback = this.dataChangedCallback.bind(this);

    this.handleSearchGo = this.handleSearchGo.bind(this);
    this.handlePageChanged = this.handlePageChanged.bind(this);

    // create data set of random length
    this.dataSet = [];
    this.pageSize = 10;
    this.pagesCount = Math.ceil(this.dataSet.length / this.pageSize);

    this.state = {
      totalPage: this.pagesCount,
      currentPage: 0,
      visiblePage: 10,
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

  dataChangedCallback() {
    this.requestFetch();
  }

  renderData() {
    const firstIdx = this.state.currentPage * this.state.visiblePage;
    const lastIdx = this.state.currentPage * this.state.visiblePage + this.state.visiblePage;
    const tableData = this.dataSet.slice(firstIdx, lastIdx);

    // console.log("firstIdx = " + firstIdx);
    // console.log("lastIdx = " + lastIdx);
    // console.log("tableData = " + tableData);

    if (this.dataSet.length === 0) {
      return (
        <tr key="row-nodata">
          <td colSpan="4" className="text-center align-middle" height="200">No Data</td>
        </tr>
      );
    } else {
      return tableData.map((data, index) => {
        return (
          <tr key={'row' + data.id}>
            <td>{data.siteName}</td>
            <td>{data.siteType}</td>
            <td><a href={data.siteUrl} target="_blank" rel="noopener noreferrer">{data.siteUrl}</a></td>
            <td>{data.myId}</td>
            <td>
              <PbFormEdit dataFromParent={data} />
              &nbsp;
              <PbFormDel dataFromParent={data} />
            </td>
          </tr>
        );
      });
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
      parentState.dataSet = resJson.data;
      parentState.setState({ keywordError: '' })
      console.log(resJson.result);
    }).catch(function (error) {
      parentState.setState({ keywordError: error.message })
      console.log(error);
    });
  }

  componentDidMount() {
    this.requestFetch();
  }

  handlePageChanged(newPage) {
    this.setState({ currentPage: newPage });
  }

  handleSearchGo(event) {
    event.preventDefault();

    var searchKey = event.target.keyword;
    if (searchKey.value === "") {
      this.setState({ keywordError: "검색할 키워드를 입력해 주세요." });
      return;
    }
    this.requestFetch(searchKey.value);
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
                    <Form onSubmit={this.handleSearchGo}>
                      <InputGroup>
                        <InputGroupAddon addonType="prepend">
                          <InputGroupText>Keyword</InputGroupText>
                        </InputGroupAddon>
                        <Input type="text" name="keyword" placeholder="Enter the search keyword" />
                        <InputGroupAddon addonType="append">
                          <Button type="submit" color="primary">Search</Button>
                        </InputGroupAddon>
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
                <i className="fa fa-align-justify"></i> Password List (Total : {this.dataSet.length})
                <PbFormAdd callbackFromParent={this.myCallback} />
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
                    {this.renderData()}
                  </tbody>
                </Table>
                <Pager
                  total={this.state.totalPage}
                  current={this.state.currentPage}
                  visiblePages={this.state.visiblePage}
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
