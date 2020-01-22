import React, { Component } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { Nav, NavItem } from 'reactstrap';
import PropTypes from 'prop-types';

import { AppNavbarBrand, AppSidebarToggler } from '@coreui/react';
import logo from '../../assets/img/brand/passwordbook.png'
import sygnet from '../../assets/img/brand/passwordbook-icon.png'

const propTypes = {
  children: PropTypes.node,
};

const defaultProps = {};

class PbHeader extends Component {
  render() {

    // eslint-disable-next-line
    const { children, ...attributes } = this.props;

    return (
      <React.Fragment>
        <AppSidebarToggler className="d-lg-none" display="md" mobile />
        <AppNavbarBrand
          full={{ src: logo, width: 30, height: 30, alt: 'PasswordBook Logo' }}
          minimized={{ src: sygnet, width: 30, height: 30, alt: 'PasswordBook Logo' }}
        />
        <AppSidebarToggler className="d-md-down-none" display="lg" />

        <Nav className="d-md-down-none" navbar>
          <NavItem className="px-3">
            <NavLink to="/dashboard" className="nav-link" >Dashboard</NavLink>
          </NavItem>
          <NavItem className="px-3">
            <Link to="/passwordbook" className="nav-link">PasswordBook</Link>
          </NavItem>
        </Nav>
        <Nav className="ml-auto" navbar>
        </Nav>
      </React.Fragment>
    );
  }
}

PbHeader.propTypes = propTypes;
PbHeader.defaultProps = defaultProps;

export default PbHeader;
