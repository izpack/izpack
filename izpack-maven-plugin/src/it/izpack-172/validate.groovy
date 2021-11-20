assert new File(basedir, 'target/izpack/install.xml').exists();


content = new File(basedir, 'target/izpack/install.xml').text;

assert content.contains('<appname>IzPack-172-it</appname>')
assert content.contains('name="org.codehaus.izpack"')
assert !content.contains('email="org.codehaus.izpack@domain.com"')
assert content.contains('@{app.version.static}')
assert !content.contains('<appversion>some.dummy.version</appversion>')
