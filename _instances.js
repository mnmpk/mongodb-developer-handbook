db.runCommand({
  collMod: "_instances",
  changeStreamPreAndPostImages: { enabled: true }
});
