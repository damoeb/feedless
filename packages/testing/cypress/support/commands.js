// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
Cypress.Commands.add('fakeLogin', () => {
  cy.clearAllCookies()
  cy.request('http://localhost:8080/testing/create-token')
  cy.getAllCookies()
    .should('have.length', 1)
  cy.reload()
  cy.get('.cy-profile-button').should('be.visible')
})

Cypress.Commands.add('getWizardOptionFeedsInHtml', () => {
  return cy.get('.feeds__html')
})

Cypress.Commands.add('getWizardOptionRefineFeed', () => {
  return cy.get('.feeds__refine')
})

Cypress.Commands.add('getWizardNextButton', () => {
  return cy.get('.cy-wizard-button__next')
})
Cypress.Commands.add('getWizardBackButton', () => {
  return cy.get('.cy-wizard-button__back')
})

// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
