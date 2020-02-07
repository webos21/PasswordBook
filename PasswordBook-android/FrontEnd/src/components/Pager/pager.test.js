import React from 'react';
import ReactDOM from 'react-dom';
import Pager from './Pager';

// Test TITLES
const TITLES = {
	first:   'First',
	prev:    '\u00AB',
	prevSet: '...',
	nextSet: '...',
	next:    '\u00BB',
	last:    'Last',
};

// Test Values
Pager.defaultProps = {
	titles: TITLES,
	current: 0,
	total: 0,
	visiblePages: 0,
};

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Pager />, div);
  ReactDOM.unmountComponentAtNode(div);
});
