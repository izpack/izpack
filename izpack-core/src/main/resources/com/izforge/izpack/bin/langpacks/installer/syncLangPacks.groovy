package com.izforge.izpack.bin.langpacks.installer

def languages = ['bgr', 'bra', 'cat', 'ces', 'chn', 'dan', 'deu', 'ell', 'eus', 'fa',
                 'fin', 'fra', 'glg', 'hun', 'idn', 'ita', 'jpn', 'kor', 'msa', 'nld',
                 'nor', 'pol', 'prt', 'ron', 'rus', 'slk', 'spa', 'srp', 'swe', 'tur',
                 'twn', 'ukr']
languages.each {syncLangPack(it)}

private void syncLangPack(String lang) {
    println "Synchronizing langpack for $lang..."
    def refStrings = getReferenceStrings(lang)
    def writer = new File(lang + '.xml').newWriter("UTF-8")
    new File('eng.xml').eachLine("UTF-8", { line ->
        writer.writeLine(getLangString(line, refStrings))
    })
    writer.flush()
    writer.close()
}

private static String getLangString(String line, def refStrings) {
    int startIndex = line.indexOf('    <str id="')
    if (startIndex == -1) {
        return line
    }
    int endIndex = line.indexOf('" txt="')
    if (endIndex == -1) {
        return line
    }
    String id = line.substring(startIndex + 13, endIndex)
    String txt = refStrings.get(id)
    if (txt != null) {
        return '    <str id="' + id + '" txt="' + txt + '"/>'
    }
    return line
}

private static def getReferenceStrings(String lang) {
    def map = [:]
    new File(lang + '.xml').eachLine("UTF-8", { line ->
        int startIndex = line.indexOf('    <str id="')
        if (startIndex == -1) {
            return
        }
        int endIndex = line.indexOf('" txt="')
        if (endIndex == -1) {
            return
        }
        String id = line.substring(startIndex + 13, endIndex)
        startIndex = endIndex + 7
        endIndex = line.length() - 3
        String txt = line.substring(startIndex, endIndex)
        map.put(id, txt)
        return
    })
    return map
}
