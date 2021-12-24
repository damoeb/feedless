import { DOMParser } from 'xmldom';
import * as xpath from 'xpath-ts';

function xvalue<T>(path: string, context: Node, optional = true): T {
  try {
    return (xpath.select(path, context, true) as any).nodeValue as T;
  } catch (e) {
    if (!optional) {
      throw new Error(`Cannot resolve ${path} in ${context}`);
    }
  }
}

function xvalues<T>(path: string, context: Node): T[] {
  return (xpath.select(path, context) as Node[]).map(
    (node) => node.nodeValue as any,
  );
}

function xselect(path: string, context: Node): Node[] {
  return xpath.select(path, context) as any;
}

export interface QueryEngine {
  name: string;
  url: string;
}
export interface OpmlHead {
  ownerEmail?: string;
  queryEngines: QueryEngine[];
}
export interface OpmlDocument {
  body: OpmlOutline[];
  head: OpmlHead;
}
export interface OpmlOutline {
  title?: string;
  xmlUrl?: string;
  htmlUrl?: string;
  pp?: string[];
  query: string;
  filter?: string;
  outlines?: OpmlOutline[];
}

export class OpmlParser {
  public parseOpml(base64: string): OpmlDocument {
    const dom = OpmlParser.parseXml(
      Buffer.from(base64, 'base64').toString('utf8'),
    );
    const body = xselect('//body', dom)[0];
    return {
      head: this.getHead(dom),
      body: this.getBuckets(body),
    };
  }

  private static parseXml(xml: string): Document {
    try {
      return new DOMParser().parseFromString(xml);
    } catch (e) {
      throw e;
    }
  }

  private getOutlines(context: Node): OpmlOutline[] {
    const outlines = xselect('./outline', context);
    return outlines.map((outline) => {
      return {
        title: xvalue<string>('@title', outline),
        htmlUrl: xvalue<string>('@htmlUrl', outline),
        xmlUrl: xvalue<string>('@xmlUrl', outline),
        query: xvalue<string>('@query', outline),
        outlines: this.getOutlines(outline),
      };
    });
  }

  private getBuckets(context: Node): OpmlOutline[] {
    const buckets = xselect('./outline', context);
    const filter = xvalue<string>('@filter', context, true);
    return buckets.map((bucket) => {
      return {
        title: xvalue<string>('@title', bucket, true),
        filter: xvalue<string>('@filter', bucket, true) || filter,
        pp: xvalues<string>('@pp', bucket),
        query: xvalue<string>('@query', bucket, true),
        outlines: this.getOutlines(bucket),
      };
    });
  }

  private getHead(dom: Document): OpmlHead {
    const head = xselect('//head', dom)[0];
    return {
      ownerEmail: xselect('./ownerEmail', head)[0].textContent,
      queryEngines: xselect('./queryEngine', head).map((queryEngine) => ({
        name: xvalue('@name', queryEngine),
        url: xvalue('@url', queryEngine),
      })),
    };
  }
}
