import React, { Component } from 'react';
import { Button, Card, CardBody, CardGroup, Col, Container, Row } from 'reactstrap';
import PbLoginForm from './PbLoginForm';

class PbLogin extends Component {
  render() {
    return (
      <div className="app flex-row align-items-center">
        <Container>
          <Row className="justify-content-center">
            <Col md="8">
              <CardGroup>
                <Card className="p-4">
                  <CardBody>
                    <PbLoginForm />
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

export default PbLogin;
