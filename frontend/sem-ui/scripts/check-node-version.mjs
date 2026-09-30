const major = Number(process.versions.node.split(".")[0]);

if (major < 24) {
  console.error(
    `sem-ui requires Node.js >= 24. Current: ${process.versions.node}`,
  );
  process.exit(1);
}
