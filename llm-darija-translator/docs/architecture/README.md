# Architecture Diagrams

All diagrams are written in PlantUML. Render them to PNG using the instructions below.

---

## Diagrams

### 1. Class Diagram (`class-diagram.puml`)
Shows the backend Java class structure:
- All packages: resource, service, llm, dto, config, security, filter, exception
- Key fields, methods, and relationships (dependencies, uses, throws)
- Annotations (CDI scope, JAX-RS, Provider) shown in stereotypes

### 2. Deployment Diagram (`deployment-diagram.puml`)
Shows the runtime topology:
- Developer machine hosts: Payara Micro (backend), PHP server, Streamlit, Expo
- Client nodes: Browser (Chrome extension, PHP UI, Python UI), Mobile device
- External cloud: Google Gemini API
- Communication protocols: HTTP, HTTPS

### 3. Sequence Diagram (`sequence-diagram.puml`)
Shows the end-to-end translation flow:
- User initiates request via any client
- BasicAuthFilter validates credentials (shows 401 alt)
- TranslationService validates input (shows 400 alt)
- GeminiTranslationClient calls Gemini API (shows 502 alt on failure)
- Successful path returns 200 with translated Darija text

---

## How to Render

### Option 1 — PlantUML Online (easiest)
1. Go to https://www.plantuml.com/plantuml/uml/
2. Paste the `.puml` file content
3. Click **Submit** to view the diagram
4. Download as PNG

### Option 2 — VS Code Extension
1. Install the "PlantUML" extension by jebbs
2. Open any `.puml` file
3. Press `Alt+D` (or `Option+D` on Mac) to preview
4. Right-click → Export → PNG

### Option 3 — Command Line (requires Java + Graphviz)
```bash
# Download plantuml.jar from https://plantuml.com/download
java -jar plantuml.jar docs/architecture/*.puml -o images/
```

### Option 4 — Docker
```bash
docker run --rm -v $(pwd):/data plantuml/plantuml \
  -tpng /data/docs/architecture/*.puml
```

---

## images/ folder
Place generated PNG exports here for the project README and presentation.
