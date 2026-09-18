# Cluster manifests

`kubectl apply -f k8s/...` against a single-node cluster. Images come from the node-local registry at `localhost:5000` (`registries.yaml`), which `scripts/deploy-feedless.sh` pushes to after `./gradlew bundle` has built them on that same host.

## Config and secrets are not in this repo

Every manifest that carries a `Secret` is tracked here as a **template** ending in `.example.yaml`, and the real file next to it is git-ignored:

| Template (tracked) | Real file (server only, ignored) |
|---|---|
| `feedless/feedless-config.example.yaml` | `feedless/feedless-config.yaml` |
| `feedless/rss-proxy-config.example.yaml` | `feedless/rss-proxy-config.yaml` |
| `feedless/untold-config.example.yaml` | `feedless/untold-config.yaml` |
| `feedless/upcoming-config.example.yaml` | `feedless/upcoming-config.yaml` |
| `plausible-config.example.yaml` | `plausible-config.yaml` |
| `plausible-ingress-config.example.yaml` | `plausible-ingress-config.yaml` |
| `postgis-config.example.yaml` | `postgis-config.yaml` |

The template's job is to document **which keys exist**; the server decides what they are set to. Add a key to the template in the same change that makes the code read it — nothing validates this at startup, so a key that is only on the server is a key the next environment silently lacks.

`loki-config.yaml` and `prometheus-config.yaml` keep their names: they hold no secrets and no per-environment values.

## Deployment never applies config

`scripts/deploy-feedless.sh` applies an explicit list of workload manifests and deliberately leaves config and secrets alone. **Do not replace that list with a glob over `k8s/feedless/`** — it would overwrite the cluster's real secrets with whatever a template happens to contain. Config changes are applied by hand:

```bash
kubectl apply -f k8s/feedless/feedless-config.yaml   # the real file, on the server
kubectl rollout restart deployment feedless-core     # env changes need a restart
```

## Adopting this on a host that predates it

The real files used to be tracked under the names now ignored, so a checkout that has them locally modified will refuse to pull. Once, on that host:

```bash
cp k8s/feedless/feedless-config.yaml /tmp/feedless-config.yaml   # and each other real file
git checkout -- k8s                                              # drop the local edits
git pull                                                         # renames land
cp /tmp/feedless-config.yaml k8s/feedless/feedless-config.yaml   # restore; now ignored
git status --short k8s                                           # expect no output
```
