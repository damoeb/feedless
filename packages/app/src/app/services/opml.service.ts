import { Injectable } from '@angular/core';
import {
  ExportOpml,
  GqlExportOpmlMutation,
  GqlExportOpmlMutationVariables,
  GqlExportOpmlResponse,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { ToastController } from '@ionic/angular';

export interface Outline {
  title: string;
  text?: string;
  xmlUrl?: string;
  htmlUrl?: string;
  outlines?: Outline[];
}

@Injectable({
  providedIn: 'root',
})
export class OpmlService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly toastCtrl: ToastController
  ) {}

  exportOpml(): Promise<Pick<GqlExportOpmlResponse, 'data'>> {
    return this.apollo
      .mutate<GqlExportOpmlMutation, GqlExportOpmlMutationVariables>({
        mutation: ExportOpml,
      })
      .then((response) => response.data.exportOpml);
  }

  async convertOpmlToJson(uploadEvent: Event): Promise<Outline[]> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      Array.from((uploadEvent.target as any).files).map((file: File) => {
        reader.onloadend = async (event) => {
          const data: ArrayBuffer | string = (event.target as any).result;
          const parser = new DOMParser();
          const doc = parser.parseFromString(String(data), 'application/xml');
          const errorNode = doc.querySelector('parsererror');
          if (errorNode) {
            console.log(errorNode);
            const toast = await this.toastCtrl.create({
              message: 'Parsing failed',
              duration: 3000,
              color: 'danger',
            });
            await toast.present();
            reject(new Error(`${errorNode}`));
          } else {
            const groups = Array.from(
              doc.documentElement.querySelectorAll('body>outline')
            );
            resolve(groups.map((group) => this.parseOutline(group)));
          }
        };
        reader.readAsText(file);
      });
    });
  }

  private parseOutline(group: Element): Outline {
    return {
      title: group.getAttribute('title'),
      text: group.getAttribute('text'),
      xmlUrl: group.getAttribute('xmlUrl'),
      htmlUrl: group.getAttribute('htmlUrl'),
      outlines: Array.from(group.children).map((child) =>
        this.parseOutline(child)
      ),
    };
  }
}
