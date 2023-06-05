/// <reference types="cypress" />

describe('logout flow', () => {

  beforeEach(() => {
    cy.visit('http://localhost:4200');
    cy.clearAllLocalStorage()
    cy.fakeLogin()
  })
  it('logout', () => {
    cy.get('.cy-profile-button')
      .should('be.visible')
      .click()
    cy.get('.cy-logout-button')
      .should('be.visible')
      .click()
    cy.getAllCookies().should('have.length', 0)
  });

})
