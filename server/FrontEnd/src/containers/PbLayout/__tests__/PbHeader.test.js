import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import PbHeader from '../PbHeader';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<MemoryRouter><PbHeader /></MemoryRouter>, div);
  ReactDOM.unmountComponentAtNode(div);
});
