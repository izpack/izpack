assert new File(basedir, 'target/staging/install.xml').exists();
assert new File(basedir, 'target/test-installer-1192.jar').exists();


content = new File(basedir, 'target/staging/install.xml').text;

assert content.contains('<appname>Test 1192</appname>')
assert content.contains('<appversion>1.2.3</appversion>')
