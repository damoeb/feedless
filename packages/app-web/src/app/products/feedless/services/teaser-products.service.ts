import { Injectable } from '@angular/core';
import products from '../../../../../meta.json'
import { marked } from 'marked';

export type ProductTeaser = {
  localSetup: string;
  id: string
  title: string
  imageUrl: string
  subtitle: string
  description: string
  costs: number,
  features: string[]
}

@Injectable()
export class TeaserProductsService {

  getProducts(): Promise<ProductTeaser[]> {
    return Promise.all(Object.values(products.apps).map(async a => ({
      ...a,
      localSetup: await marked(a.localSetup),
      imageUrl: `/assets/${a.id}.jpeg`
    })));
  }
}
