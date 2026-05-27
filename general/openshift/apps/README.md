# Quarkus apps on OpenShift (`kafka-demo`)

Deploy **data-generator** and **replicate-redis** into namespace `kafka-demo`.

## Prerequisites

- `postgres-src`, `redis`, and Kafka (`my-cluster`) are running in `kafka-demo`
- OpenShift CLI logged in

## 1. Namespace and builds

```bash
oc apply -f general/openshift/apps/namespace.yaml
oc apply -n kafka-demo -f general/openshift/apps/builds.yaml
```

Build images (run `mvn package` first so `target/quarkus-app/` exists):

```bash
cd data-generator
mvn package -DskipTests
oc start-build data-generator --from-dir=. --follow -n kafka-demo

cd ../replicate-redis
mvn package -DskipTests
oc start-build replicate-redis --from-dir=. --follow -n kafka-demo
```

## 2. Deploy applications

Deployments pull from the **OpenShift internal registry** (not Docker Hub):

`image-registry.openshift-image-registry.svc:5000/kafka-demo/<app>:latest`

```bash
oc apply -n kafka-demo -f general/openshift/apps/data-generator.yaml
oc apply -n kafka-demo -f general/openshift/apps/replicate-redis.yaml
```

If the image was built before the Deployment, restart after build completes:

```bash
oc rollout restart deployment/data-generator deployment/replicate-redis -n kafka-demo
```

## 3. Verify

```bash
oc get pods -n kafka-demo -l app=data-generator
oc logs -n kafka-demo deploy/data-generator --tail=80
oc get endpoints data-generator -n kafka-demo
oc get route data-generator -n kafka-demo
```

If the Route shows **Application is not available**, the Pod is not Ready (often DB connection or a stale image). Check logs first.

After changing `data-generator` code or `pom.xml`, rebuild the image and restart:

```bash
cd data-generator && mvn package -DskipTests
oc start-build data-generator --from-dir=. --follow -n kafka-demo
oc apply -n kafka-demo -f general/openshift/apps/data-generator.yaml
oc rollout restart deployment/data-generator -n kafka-demo
```

- Swagger UI: `https://<route>/q/swagger-ui`
- API: `https://<route>/generate-data`

Redis (after CDC events):

```bash
oc exec -n kafka-demo deploy/redis -- redis-cli KEYS '*'
```

## Configuration

| App | Connects to |
|-----|-------------|
| data-generator | `postgres-src:5432` / `example_db` |
| replicate-redis | Kafka `my-cluster-kafka-bootstrap:9092`, Redis `redis:6379` |

Edit ConfigMaps `data-generator-config` / `replicate-redis-config` and restart Deployments to change settings.

## 4. Use Red Hat Dev Spaces for hands-on

This repository includes a root `devfile.yaml` for a **tooling-only** workspace.

- The devfile does **not** create Kafka/PostgreSQL/Redis or any other runtime components.
- Build/deploy of those components is performed manually during the hands-on.
- `data-generator` and `replicate-redis` should be cloned manually from GitHub in the workspace terminal.

### Quick start

1. In Red Hat Dev Spaces, create a workspace from this Git repository.
2. Keep the workspace in the same OpenShift cluster/namespace where hands-on components are reachable.
3. Run `check-tools` command from the Dev Spaces command palette/UI.
4. Clone application repositories manually:

```bash
git clone <data-generator-repo-url> data-generator
git clone <replicate-redis-repo-url> replicate-redis
```
