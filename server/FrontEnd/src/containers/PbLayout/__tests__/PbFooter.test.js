import React from 'react';
import ReactDOM from 'react-dom';
import PbFooter from '../PbFooter';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<PbFooter />, div);
  ReactDOM.unmountComponentAtNode(div);
});
