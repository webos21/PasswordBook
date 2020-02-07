import React from 'react';
import { withRouter } from "react-router-dom";
import { Button, Col, Row, Form, InputGroup, InputGroupAddon, InputGroupText } from 'reactstrap';
import { useForm } from "react-hook-form";
import Cookies from 'universal-cookie';

const PbLoginForm = props => {
    const crypto = require('crypto');

    const { register, handleSubmit, errors, setError } = useForm({
        submitFocusError: true,
        nativeValidation: false,
    });

    const onSubmit = (data, e) => {
        var iv = Buffer.from("PasswordBook1234");
        var sha256 = crypto.createHash('sha256');
        sha256.update('PasswordBook');

        // console.log("iv = " + iv);
        // console.log("sha256 = " + sha256);
        // console.log("e.target.pbpwd.value = " + e.target.pbpwd.value);
    
        var aesCipher = crypto.createCipheriv('aes-256-cbc', sha256.digest(), iv);
        aesCipher.update(e.target.pbpwd.value);
        var cryptBytes = aesCipher.final();
        var base64Result = cryptBytes.toString('base64');

        // console.log("base64Result = " + base64Result);

        e.target.pbpwd.value = base64Result;

        const formData = new FormData(e.target);

        formData.pbpwd = base64Result;
        fetch('/login.do', {
            method: 'POST',
            body: formData,
        }).then(function (res) {
            // console.log(res.status);
            // console.log(res.statusText);
            // console.log(res.headers);
            // console.log(res.url);

            if (!res.ok) {
                throw Error("서버응답 : " + res.statusText + "(" + res.status + ")");
            }
            return res.json();
        }).then(function (resJson) {
            console.log(resJson.result);
            if (resJson.auth && resJson.auth.ckey && resJson.auth.cval) {
                let cookies = new Cookies();
                cookies.set(resJson.auth.ckey, resJson.auth.cval);
                props.history.push('/');
            }
        }).catch(function (error) {
            setError("pbpwd", "serverResponse", error.message);
            console.log(error);
            e.target.reset();
        });
    };

    return (
        <Form onSubmit={handleSubmit(onSubmit)}>
            <h1>Login</h1>
            <p className="text-muted">Sign In to your PasswordBook</p>
            <Row className="form-group">
                <InputGroup>
                    <InputGroupAddon addonType="prepend">
                        <InputGroupText>
                            <i className="icon-lock"></i>
                        </InputGroupText>
                    </InputGroupAddon>
                    <input
                        type="password"
                        className={"form-control" + (errors.pbpwd ? " is-invalid" : " is-valid") }
                        name="pbpwd"
                        placeholder="Password"
                        tabIndex="0"
                        ref={register({
                            required: true,
                            minLength: 4,
                            maxLength: 16,
                        })}
                    />
                </InputGroup>

                {errors.pbpwd && errors.pbpwd.type === "required" && <small id="pbpwdError" className="text-danger">비밀번호를 입력해 주세요.</small>}
                {errors.pbpwd && errors.pbpwd.type === "minLength" && <small id="pbpwdError" className="text-danger">비밀번호는 4자 이상입니다.</small>}
                {errors.pbpwd && errors.pbpwd.type === "serverResponse" && <small id="pbpwdError" className="text-danger">{errors.pbpwd.message}</small>}
            </Row>
            <Row className="mt-2">
                <Col xs="6">
                    <Button type="submit" color="primary" className="px-4">Login</Button>
                </Col>
            </Row>
        </Form>
    );
};

export default withRouter(PbLoginForm);
