import React from 'react';
import { Link } from 'react-router-dom';
import { Nav, Navbar, Button, Image } from 'react-bootstrap';
import styled from 'styled-components';

const loginApi = 'https://api.plusmed.cloud/signIn'

const Styles = styled.div`
  .navbar {
    background-color: transparent;
  }
  a, .navbar-nav .nav-link {
    color: #bbb;
    &:hover {
      color: white;
    }
  }
  .navbar-brand { 
    display: flex;
    align-items: center;
    color: #bbb;
    &:hover {
      color: white;
    }
  }
  .navbar-brand>img {
    margin: 3px 14px;
  }
  .btn-primary, .btn-primary:hover, .btn-primary:active, .btn-primary:visited {
    background: #12736C !important;
    color: white !important;
    font-size: 1em !important;
    margin-left: 1em !important;
    padding: 0.25em 1em !important;
    border: 2px solid #12736C !important;
    border-radius: 3px !important;
    transition: 0.5s all ease-out !important;

    &:hover {
      background: #4da29a !important;
      color: white !important;
      font-size: 1em !important;
      margin-left: 1em !important;
      padding: 0.25em 1em !important;
      border: 2px solid #4da29a !important;
      border-radius: 3px;  !important
    }
  }
  .btn-nav-login {
    margin-top: 2px;
  }
  .nav-item > .external-nav-link {
    &:hover {
      color: #bbb;
    }
    &:visited {
      color: #bbb;
    }
  }
`

const Name = styled.h1`
  font-size: 1.25em;
  margin-left: 20%;
  color: #bbb;
  white-space: nowrap;
  padding: 0.4em 1em !important;
  &:hover {
    color: white;
  }

`;

// todo img

export const NavigationBarLogged = ({ userData, history }) => {
  const docPub = 'images/doctors/'; // FIXME do zmiany

  let name = userData.name + ' ' + userData.surname;
  let avatar = userData.avatarPath;

  function handleSubmit(event) {
    localStorage.removeItem('user');
    localStorage.removeItem('userData');
    history.push('/');
    event.preventDefault();
  }

  return (
    <Styles>
      <Navbar fixed="top" expand="lg" bg="dark" variant="dark">
        <Link to="/">
          <Navbar.Brand>
            <img
              alt=""
              src={require('../assets/plus.svg')}
              width="30"
              height="30"
              class="img-responsive"
            ></img>{' '}
            PlusMed
          </Navbar.Brand>
        </Link>
        <Link to="/profile">
          <Navbar.Brand>
            <Image src={docPub + avatar} roundedCircle style={{ marginLeft: "4rem", width: "30px", height: "30px" }} />{' '}{name}
          </Navbar.Brand>
        </Link>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ml-auto">
            <Nav.Item>
              <Link class="nav-link" to="/priceList">Cennik</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/about">Specjaliści</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/offer">Oferta</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/contact">Kontakt</Link>
            </Nav.Item>
            <Nav.Item style={{ margin: "7px 20px 0 20px" }}>
              &nbsp;•&nbsp;
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/profile">Profil</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/visits">Wizyty</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/prescriptions">Recepty</Link>
            </Nav.Item>
            <Nav.Item>
              <Link class="nav-link" to="/referrals">Skierowania</Link>
            </Nav.Item>
            <Nav.Item>
              <Button onClick={handleSubmit.bind(this)} className="btn-nav-login">Wyloguj</Button>
            </Nav.Item>
          </Nav>
        </Navbar.Collapse>
      </Navbar>
    </Styles>
  )
}

export default NavigationBarLogged;
