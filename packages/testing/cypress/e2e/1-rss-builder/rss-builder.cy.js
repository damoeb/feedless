/// <reference types="cypress" />

// https://on.cypress.io/introduction-to-cypress

describe('builder', () => {
  beforeEach(() => {
    // cy.visit('http://localhost:4200?forceProduct=rssProxy');
    cy.visit('http://localhost:4200');
    cy.clearAllCookies();
    cy.get('.cy-product-header').should('contain.text', 'RSS Builder');
  })

  function submitUrl(url) {
    cy.get('.cy-searchbar-input').type(`${url}`);
    cy.get('.cy-searchbar-button').click();
  }

  it('with dynamic website', () => {
    submitUrl('http://localhost:8080/testing/file/dynamic-website.html');
    cy.get('.cy-enable-js-button').click();
  })

  // it('with feed', () => {
  //   submitUrl('http://localhost:8080/testing/file/atom.xml')
  // })
})
