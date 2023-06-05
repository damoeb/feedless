/// <reference types="cypress" />

describe('feed', () => {

  beforeEach(() => {
    cy.visit('http://localhost:4200');
    cy.fakeLogin()
  })
  it('from header', () => {
    cy.get('.cy-create-feed-button')
      .should('be.visible')
      .click()
    cy.get('.modal-dialog')
      .should('be.visible')
    const url = 'www.heise.de/rss/heise-atom.xml';
    cy.get('.cy-website-or-feed-input').type(`${url}{enter}`)
    cy.getWizardOptionRefineFeed().click()
    cy.get('.cy-remote-feed').should('exist')
    cy.getWizardNextButton().click()
    cy.getWizardNextButton()
      .should('have.text', 'Save')
      .click()
    cy.get('.cy-wait-for-articles').should('be.visible')
    cy.get('.cy-article-ref')
      .should('have.length.above', 0)

    cy.get('.cy-delete-bucket-button').click()
  });

})
