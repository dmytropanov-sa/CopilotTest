Run backend tests with JaCoCo coverage

This project uses the `jacoco-maven-plugin` to collect code coverage for unit tests and generate an HTML report.

Commands

- Run tests and produce coverage report:

```powershell
mvn -f backend/ verify
```

- Open coverage report (Windows):

```powershell
start backend\target\site\jacoco\index.html
```

Notes

- The plugin attaches an agent during the test phase and generates the HTML report during the `verify` phase.
- Coverage data is written to `backend/target/site/jacoco` and `backend/target/jacoco.exec`.
 
Jacoco check

- A `jacoco:check` goal is configured to run during `verify` and enforces a minimum of 80% line coverage for the bundle. The `mvn verify` step will fail if the overall line coverage is below 80%.
