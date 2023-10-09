import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { ToastController } from '@ionic/angular';

export interface Agent {
  id: string
  name: string
  personal: boolean
  online: boolean
}

@Injectable({
  providedIn: 'root',
})
export class AgentService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly toastCtrl: ToastController,
  ) {}

  getAgents(): Agent[] {
    return [
      {
        id: 'laptop',
        name: 'laptop',
        personal: true,
        online: true
      }
    ]
  }

}
