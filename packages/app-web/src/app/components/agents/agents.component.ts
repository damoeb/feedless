import { Component, OnInit } from '@angular/core';
import { Agent, AgentService } from '../../services/agent.service';
import { dateTimeFormat } from '../../services/profile.service';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.scss'],
})
export class AgentsComponent implements OnInit {
  agents: Agent[] = [];

  constructor(private readonly agentService: AgentService) {}

  async ngOnInit() {
    this.agents = await this.agentService.getAgents();
  }

  protected readonly dateTimeFormat = dateTimeFormat;
}
