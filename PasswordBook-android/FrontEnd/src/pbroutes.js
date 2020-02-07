import React from 'react';

const Dashboard = React.lazy(() => import('./views/Dashboard/Dashboard'));
const PasswordBook = React.lazy(() => import('./views/PasswordBook/PasswordBook'));
const PbLogout = React.lazy(() => import('./views/PbLogout/PbLogout'));

// https://github.com/ReactTraining/react-router/tree/master/packages/react-router-config
const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/passwordbook', exact: true,  name: 'PasswordBook', component: PasswordBook },
  { path: '/logout', exact: true,  name: 'PbLogout', component: PbLogout },
];

export default routes;
