import React, { Component } from 'react';
import { withRouter } from "react-router-dom";
import Cookies from 'universal-cookie';
import { Col, Container, Row } from 'reactstrap';

class PbLogout extends Component {

  componentDidMount() {
    let cookies = new Cookies();
    cookies.remove('X-PB-AUTH');
    this.props.history.push('/');
  }

  render() {
    return (
      <div className="app flex-row align-items-center">
        <Container>
          <Row className="justify-content-center">
            <Col md="6">
              <span className="clearfix">
                <h1 className="float-left display-3 mr-4">Logout...</h1>
                <h4 className="pt-3">Remove the login information...</h4>
                <p className="text-muted float-left">The page you are looking for is temporarily unavailable.</p>
              </span>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default withRouter(PbLogout);
