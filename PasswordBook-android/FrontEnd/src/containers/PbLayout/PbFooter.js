import React, { Component } from 'react';
import PropTypes from 'prop-types';

const propTypes = {
  children: PropTypes.node,
};

const defaultProps = {};

class PbFooter extends Component {
  render() {

    // eslint-disable-next-line
    const { children, ...attributes } = this.props;

    return (
      <React.Fragment>
        <span><a href="https://webos21.github.io/PasswordBook/">PasswordBook</a> &copy; 2020 NeoFoundation.</span>
        <span className="ml-auto">Powered by <a href="https://coreui.io/react">CoreUI for React</a></span>
      </React.Fragment>
    );
  }
}

PbFooter.propTypes = propTypes;
PbFooter.defaultProps = defaultProps;

export default PbFooter;
