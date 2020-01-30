import React, { Component } from 'react';
import { Button, Card, CardBody, CardGroup, Col, Container, Form, Input, InputGroup, InputGroupAddon, InputGroupText, Row } from 'reactstrap';

class Login extends Component {
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit(event) {
    event.preventDefault();

    const data = new FormData(event.target);

    fetch('http://0.0.0.0:28080/login.do', {
      method: 'POST',
      body: data,
    }).then(function (res) {
      // console.log(res.status);
      // console.log(res.statusText);
      // console.log(res.headers);
      // console.log(res.url);

      return res.json();
    }, function (error) {
      console.log(error.message);
    }).then(function (resJson) {
      console.log(resJson.result);
    }).catch(function (error) {
      console.log("error---", error)
    });

  }

  render() {
    return (
      <div className="app flex-row align-items-center">
        <Container>
          <Row className="justify-content-center">
            <Col md="8">
              <CardGroup>
                <Card className="p-4">
                  <CardBody>
                    <Form onSubmit={this.handleSubmit}>
                      <h1>Login</h1>
                      <p className="text-muted">Sign In to your PasswordBook</p>
                      <br />
                      <InputGroup className="mb-4">
                        <InputGroupAddon addonType="prepend">
                          <InputGroupText>
                            <i className="icon-lock"></i>
                          </InputGroupText>
                        </InputGroupAddon>
                        <Input type="password" name="pbpwd" placeholder="Password"
                        minLength="4" maxLength="16" tabIndex="0" autoFocus required />
                      </InputGroup>
                      <Row>
                        <Col xs="6">
                          <Button type="submit" color="primary" className="px-4">Login</Button>
                        </Col>
                      </Row>
                    </Form>
                  </CardBody>
                </Card>
                <Card className="text-white bg-primary py-5 d-md-down-none" style={{ width: '44%' }}>
                  <CardBody className="text-center">
                    <div>
                      <h2>PasswordBook Web</h2>
                      <hr />
                      <p className="text-left">암호책 App은 비밀번호를 보관하게 하는 유용한 도구입니다.
                         여기에 사용 편의성을 돕는 웹페이지를 App이 서비스 해 줍니다.</p>
                      <a href="https://webos21.github.io/PasswordBook" target="_blank" rel="noopener noreferrer">
                        <Button color="primary" className="mt-3" active tabIndex={-1}>홈페이지 가기</Button>
                      </a>
                    </div>
                  </CardBody>
                </Card>
              </CardGroup>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default Login;
