import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import PbLogout from './PbLogout';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<MemoryRouter><PbLogout/></MemoryRouter>, div);
  ReactDOM.unmountComponentAtNode(div);
});
