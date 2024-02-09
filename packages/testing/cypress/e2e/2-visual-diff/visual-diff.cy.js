/// <reference types="cypress" />

// https://on.cypress.io/introduction-to-cypress

describe('builder', () => {
  beforeEach(() => {
    cy.visit('http://localhost:4200?forceProduct=visualDiff');
    cy.clearAllCookies();
    cy.get('.cy-product-header').should('contain.text', 'VisualDiff');
  })

  function submitUrl(url) {
    cy.get('.cy-searchbar-input').type(`${url}`);
    cy.get('.cy-searchbar-button').click();
  }

  it('with dynamic website', () => {
    submitUrl('http://localhost:8080/testing/file/dynamic-website.html');
    cy.get('.cy-email-input').type(`foo@bar`);
    cy.get('.cy-subject-input').type(`Track this page`);
    cy.get('.cy-start-monitoring-button').click();
  })

})
