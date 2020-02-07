import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Form, FormGroup, FormFeedback } from 'reactstrap';
import { useForm } from "react-hook-form";

const PbFormDel = props => {

    const initValues = props.dataFromParent;

    const dateFormat = (dateObj) => {
        var year = dateObj.getFullYear();
        var month = dateObj.getMonth() + 1;
        if (month < 10) {
            month = '0' + month;
        }
        var date = dateObj.getDate();
        if (date < 10) {
            date = '0' + date;
        }
        return year + "-" + month + "-" + date
    }

    const { register, handleSubmit, errors, setError } = useForm({
        submitFocusError: true,
        nativeValidation: false,
        defaultValues: {
            siteId: initValues.id,
        }
    });

    const [modalShow, setModalShow] = useState(false);

    const toggleOpen = () => {
        setModalShow(!modalShow);
    }

    const onSubmit = (data, e) => {
        fetch('/pwdata.do?siteId=' + data.siteId, {
            method: 'DELETE',
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
            console.log("PbFormDel::fetch => " + resJson.result);
            if (resJson.result === "OK") {
                //toggleOpen();
                props.callbackFromParent();
            }
        }).catch(function (error) {
            console.log("PbFormDel::fetch => " + error);
            setError("siteId", "serverResponse", error.message);
        });
    };

    return (
        <span>
            <Button color="danger" className="btn-sm" onClick={toggleOpen}>
                <i className="fa fa-trash-o"></i>&nbsp;삭제</Button>
            <Modal isOpen={modalShow} toggle={toggleOpen}
                className={'modal-danger ' + props.className}>
                <Form onSubmit={handleSubmit(onSubmit)}>
                    <ModalHeader toggle={toggleOpen}>비밀번호 삭제</ModalHeader>
                    <ModalBody>
                        <p>다음 항목을 삭제할까요?</p>
                        <ul>
                            <li>항목 URL : {props.dataFromParent.siteUrl}</li>
                            <li>항목 이름 : {props.dataFromParent.siteName}</li>
                            <li>항목 유형 : {props.dataFromParent.siteType}</li>
                            <li>항목 아이디 : {props.dataFromParent.myId}</li>
                            <li>항목 등록일 : {dateFormat(new Date(props.dataFromParent.regDate))}</li>
                        </ul>
                        <FormGroup>
                            <input type="hidden" name="siteId" ref={register} />
                            {errors.siteId && <FormFeedback>{errors.siteId.message}</FormFeedback>}
                        </FormGroup>
                    </ModalBody>
                    <ModalFooter>
                        <Button type="submit" color="danger">삭제</Button>{' '}
                        <Button color="secondary" onClick={toggleOpen}>취소</Button>
                    </ModalFooter>
                </Form>
            </Modal>
        </span>
    );
};

export default PbFormDel;
