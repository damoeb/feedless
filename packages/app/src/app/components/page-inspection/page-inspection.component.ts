import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-page-inspection',
  templateUrl: './page-inspection.component.html',
  styleUrls: ['./page-inspection.component.scss'],
  // changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageInspectionComponent implements OnInit {
  url: string;
  staticSource = true;

  @ViewChild('iframeElement', {static: false}) iframeRef: ElementRef;
  iframeLoaded = false;
  isLoading = false;

  constructor(
    private readonly modalController: ModalController,
    // private changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit() {}

  async dismissModal() {
    await this.modalController.dismiss();
  }

  toggleSourceType() {
    this.staticSource = !this.staticSource;
  }

  public onIframeLoad(): void {
    // if (this.rules) {
    //   this.updateScores();
    // } else {
    //   this.iframeLoaded = true;
    // }
  }

  // private highlightRule(rule: ArticleRule): void {
  //   const iframeDocument = this.iframeRef.nativeElement.contentDocument;
  //   const id = 'rss-proxy-style';
  //
  //   try {
  //     iframeDocument.getElementById(id).remove();
  //   } catch (e) {
  //
  //   }
  //   const styleNode = iframeDocument.createElement('style');
  //   styleNode.setAttribute('type', 'text/css');
  //   styleNode.setAttribute('id', id);
  //   const allMatches: HTMLElement[] = this.evaluateXPathInIframe(rule.contextXPath, iframeDocument);
  //
  //   const qualifiedAsArticle = (elem: HTMLElement): boolean => {
  //     // todo apply filters
  //     return FeedParser.qualifiesAsArticle(elem, rule, iframeDocument);
  //   };
  //   const matchingIndexes = allMatches
  //     .map(elem => {
  //       const index = Array.from(elem.parentElement.children)
  //         .findIndex(otherElem => otherElem === elem);
  //       const qualified = qualifiedAsArticle(elem);
  //       if (qualified) {
  //         console.log(`Keeping element ${index}`, elem);
  //       } else {
  //         console.log(`Removing unqualified element ${index}`, elem);
  //       }
  //       return {elem, index, qualified} as ArticleCandidate;
  //     })
  //     .filter(candidate => candidate.qualified)
  //     .map(candidate => candidate.index);
  //
  //   const cssSelectorContextPath = 'body>' + FeedParser.getRelativeCssPath(allMatches[0], iframeDocument.body, true);
  //   console.log(cssSelectorContextPath);
  //   const code = `${matchingIndexes.map(index => `${cssSelectorContextPath}:nth-child(${index + 1})`).join(', ')} {
  //           border: 3px dotted red!important;
  //           margin-bottom: 5px!important;
  //           display: block;
  //         }
  //         `;
  //
  //   const firstMatch = allMatches[0];
  //   if (firstMatch) {
  //     firstMatch.scrollIntoView();
  //   }
  //
  //   styleNode.appendChild(iframeDocument.createTextNode(code));
  //   iframeDocument.head.appendChild(styleNode);
  // }


  // public updateScores(): void {
  //   const iframeDocument = this.iframeRef.nativeElement.contentDocument;
  //   this.rules.forEach(rule => {
  //     const articles = this.evaluateXPathInIframe(rule.contextXPath, iframeDocument)
  //         // remove hidden articles
  //         .filter((elem: any) => !!(elem.offsetWidth || elem.offsetHeight))
  //       // remove empty articles
  //       // .filter((elem: any) => elem.textContent.trim() > 0)
  //       // .filter((elem: any) => Array.from(elem.querySelectorAll(rule.linkPath)).length > 0);
  //     ;
  //     if (articles.length === 0) {
  //       rule.score -= 20;
  //       // rule.hidden = true;
  //     }
  //   });
  //   this.changeDetectorRef.detectChanges();
  // }

  private evaluateXPathInIframe(xPath: string, context: HTMLElement | Document): HTMLElement[] {
    const iframeDocument = this.iframeRef.nativeElement.contentDocument;
    const xpathResult = iframeDocument.evaluate(xPath, context, null, 5);
    const nodes: HTMLElement[] = [];
    let node = xpathResult.iterateNext();
    while (node) {
      nodes.push(node as HTMLElement);
      node = xpathResult.iterateNext();
    }
    return nodes;
  }


  save() {

  }
}
