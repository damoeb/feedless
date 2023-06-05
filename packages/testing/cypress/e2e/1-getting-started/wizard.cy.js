/// <reference types="cypress" />

// https://on.cypress.io/introduction-to-cypress

describe('wizard', () => {
  beforeEach(() => {
    // Cypress starts out with a blank slate for each test
    // so we must tell it to visit our website with the `cy.visit()` command.
    // Since we want to visit the same URL at the start of all our tests,
    // we include it in our beforeEach function so that it runs before each test
    cy.visit('http://localhost:4200');
    cy.clearAllCookies()
  })

  // it('first visitor is redirected to this', () => {
  //     cy.url().should('eq', 'http://localhost:4200/getting-started')
  // });

  it('unauthenticated wizard works', () => {
    const url = 'en.wikinews.org/wiki/Main_Page';
    cy.get('.cy-searchbar-input').type(`${url}{enter}`)

    cy.get('.modal-dialog').should('be.visible')
    cy.getWizardOptionFeedsInHtml().click()
    cy.get('.cy-option-prerender')
      .should('be.visible')
      .click()

    cy.get('.cy-option-prerender')
      .should('have.class', 'cy-option-prerender--busy')

    cy.get('.cy-option-prerender')
      .should('not.have.class', 'cy-option-prerender--busy')

    cy.get('.cy-feed-native').first().click()
    cy.getWizardNextButton().click()
    cy.get('.cy-remote-feed').should('exist')
    cy.getWizardBackButton().click()

    cy.get('.cy-feed-generic').first().click()
    cy.getWizardNextButton().click()
    cy.get('.cy-remote-feed--busy').should('exist')
    cy.get('.cy-option-filter-items').click()
    cy.get('.cy-remote-feed')
      .should('exist')
      .get('.cy-remote-feed__title')
      .first()
      .then((firstTitle) => {
        const firstToken = firstTitle.text()
          .replace(/\S/, ' ')
          .split(' ')
          .filter(t => t.length > 2)
          .sort(token => token.length)[0]
        cy.get('.cy-field-filter-expression')
          .click()
          .type(`not(contains(#title, "${firstToken}"))`)
          .trigger('change')
      })

    cy.get('.cy-refresh-remote-feed')
      .should('be.visible')
      .click()
    cy.get('.cy-remote-feed--busy').should('exist')
    cy.get('.cy-remote-feed--busy').should('not.exist')
    cy.get('.cy-feed-item.item--omitted')
      .should('have.length.above', 0)

  })
})
