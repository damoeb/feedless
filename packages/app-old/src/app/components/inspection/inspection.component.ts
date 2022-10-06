import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import URI from 'urijs';

@Component({
  selector: 'app-inspection',
  templateUrl: './inspection.component.html',
  styleUrls: ['./inspection.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectionComponent implements OnInit, OnChanges {
  @Input()
  url: string;
  staticSource = true;

  @ViewChild('iframeElement', { static: false }) iframeRef: ElementRef;
  iframeLoaded = false;
  isLoading = false;

  constructor(
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly httpClient: HttpClient
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    if (changes.url) {
      const { currentValue } = changes.url;
      this.parseUrl(currentValue);
      if (this.url) {
        this.httpClient
          .get(`/api/http/get`, { params: { url: this.url } })
          .subscribe((response) =>
            this.handleHttpGetResponse(response, this.url)
          );
      }
    }
  }

  ngOnInit() {}

  private handleHttpGetResponse(response, url: string) {
    const html = this.patchHtml(response.responseBody, url);
    this.assignToIframe(html);
  }

  private assignToIframe(html: string) {
    this.iframeRef.nativeElement.src = window.URL.createObjectURL(
      new Blob([html], {
        type: 'text/html',
      })
    );
    this.changeDetectorRef.detectChanges();
  }

  private patchHtml(html: string, url: string): string {
    const doc = new DOMParser().parseFromString(html, 'text/html');

    const base = doc.createElement('base');
    base.setAttribute('href', url);
    doc.getElementsByTagName('head').item(0).appendChild(base);

    Array.from(doc.querySelectorAll('[href]')).forEach((el) => {
      try {
        const absoluteUrl = new URI(el.getAttribute('href')).absoluteTo(url);
        el.setAttribute('href', absoluteUrl.toString());
      } catch (e) {
        // console.error(e);
      }
    });

    return doc.documentElement.innerHTML;
  }

  toggleSourceType() {
    this.staticSource = !this.staticSource;
  }

  public onIframeLoad(): void {
    // if (this.rules) {
    this.updateScores();
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

  public updateScores(): void {
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
  }

  private evaluateXPathInIframe(
    xPath: string,
    context: HTMLElement | Document
  ): HTMLElement[] {
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

  save() {}

  private parseUrl(url: string) {
    if (this.isUrl(url)) {
      this.url = url;
    } else if (this.isUrl(`https://${url}`)) {
      this.url = `https://${url}`;
    } else {
      this.url = null;
    }
  }
  private isUrl(url: string) {
    try {
      const parsed = new URL(url);
      return parsed != null;
    } catch (e) {
      return false;
    }
  }
}
