import React, { Component } from 'react';
import { HashRouter, Route, Switch, Redirect } from 'react-router-dom';
// import { renderRoutes } from 'react-router-config';
import './App.scss';

const isAuthenticated = () => {
  let data = JSON.parse(sessionStorage.getItem('userData'));
  return (data !== null);
}

const UnauthenticatedRoute = ({ component: Component, ...rest }) => (
  <Route {...rest} render={(props) => (
    !isAuthenticated()
      ? <Component {...props} />
      : <Redirect to='/' />
  )} />
);

const AuthenticatedRoute = ({ component: Component, ...rest }) => (
  <Route {...rest} render={(props) => (
    isAuthenticated()
      ? <Component {...props} />
      : <Redirect to='/login' />
  )} />
);

const loading = () => <div className="animated fadeIn pt-3 text-center">Loading...</div>;

// Containers
const PbLayout = React.lazy(() => import('./containers/PbLayout'));

// Pages
const Page500 = React.lazy(() => import('./views/Pages/Page500'));
const PbLogin = React.lazy(() => import('./views/Pages/PbLogin'));

class App extends Component {

  render() {
    return (
      <HashRouter>
          <React.Suspense fallback={loading()}>
            <Switch>
              <UnauthenticatedRoute exact path="/login" name="Login Page" component={PbLogin} />
              <AuthenticatedRoute path="/" name="Home" component={PbLayout}/>} />
              <Route exact path="/500" name="Page 500" component={Page500} />
            </Switch>
          </React.Suspense>
      </HashRouter>
    );
  }
}

export default App;
