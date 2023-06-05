/// <reference types="cypress" />

describe('login flow', () => {

  describe('routing', () => {
    beforeEach(() => {
      cy.visit('http://localhost:4200');
      cy.clearAllCookies()
    })
    it('from header', () => {
      cy.get('.cy-login-button').click()
      cy.url().should('eq', 'http://localhost:4200/login')
    });
  });

})
