import React, { Component } from 'react';
import {
  Badge,
  Button,
  Card,
  CardBody,
  CardHeader,
  Col,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
  Row,
  Table
} from 'reactstrap';
import Pager from '../../components/Pager/pager.js';

class PasswordBook extends Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.toggleFade = this.toggleFade.bind(this);
    this.handlePageChanged = this.handlePageChanged.bind(this);

    // create data set of random length
    this.dataSet = [...Array(Math.ceil(500 + Math.random() * 500))].map(
      (a, i) => "Record " + (i + 1)
    );
    this.pageSize = 10;
    this.pagesCount = Math.ceil(this.dataSet.length / this.pageSize);

    this.state = {
			totalPage: this.pagesCount,
			currentPage: 0,
			visiblePage: 7,
      collapse: true,
      fadeIn: true,
      timeout: 300
    };
  }

  toggle() {
    this.setState({ collapse: !this.state.collapse });
  }

  toggleFade() {
    this.setState((prevState) => { return { fadeIn: !prevState }});
  }

  renderData() {
    const firstIdx = this.state.currentPage * this.state.visiblePage;
    const lastIdx = this.state.currentPage * this.state.visiblePage + this.state.visiblePage;
    const tableData = this.dataSet.slice(firstIdx, lastIdx);

    console.log("firstIdx = " + firstIdx);
    console.log("lastIdx = " + lastIdx);
    console.log("tableData = " + tableData);

    if (this.dataSet.length === 0) {
      return (
        <tr key="row-nodata">
          <td colSpan="4" className="text-center align-middle" height="200">No Data</td>
        </tr>
      );
    } else {
      return tableData.map((data, index) => {
        return (
          <tr key={'row'+data}>
            <td>{data}</td>
            <td>2012/01/01</td>
            <td>{index}</td>
            <td>
              <Badge color="success">Active</Badge>
            </td>
          </tr>
        );
      });
    }
  }

  handlePageChanged(newPage) {
		this.setState({ currentPage : newPage });
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
                    <FormGroup>
                      <div className="search">
                        <InputGroup>
                          <InputGroupAddon addonType="prepend">
                            <InputGroupText>Keyword</InputGroupText>
                          </InputGroupAddon>
                          <Input type="text" id="keyword" placeholder="Enter the search keyword" required />
                          <InputGroupAddon addonType="append">
                            <Button color="primary">Search</Button>
                          </InputGroupAddon>
                        </InputGroup>
                      </div>
                    </FormGroup>
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
              </CardHeader>
              <CardBody>
                <Table hover bordered striped responsive size="sm">
                  <thead>
                  <tr>
                    <th>Username</th>
                    <th>Date registered</th>
                    <th>Role</th>
                    <th>Status</th>
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
