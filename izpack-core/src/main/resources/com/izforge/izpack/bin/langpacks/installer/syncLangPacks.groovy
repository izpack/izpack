import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

def languages = ['bgr', 'bra', 'cat', 'ces', 'chn', 'dan', 'deu', 'ell', 'eus', 'fa',
                 'fin', 'fra', 'glg', 'hun', 'idn', 'ita', 'jpn', 'kor', 'msa', 'nld',
                 'nor', 'pol', 'prt', 'ron', 'rus', 'slk', 'spa', 'srp', 'swe', 'tur',
                 'twn', 'ukr']
languages.each {syncLangPack(it)}

private void syncLangPack(String lang) {
    println "Synchronizing langpack for $lang..."
    def langpack = getEnglishLangPack()
    use(DOMCategory) {
        def strings = langpack.str
        def refStrings = getReferenceStrings(lang)
        for (def str in strings) {
            def id = str.attributes['id']
            def txt = findRefString(refStrings, id)
            if (txt) {
                str['@txt'] = txt
            }
        }
    }
    new File(lang + '.xml').write(langpack as String, 'UTF-8')
}

private def getReferenceStrings(String lang) {
    def input = new File(lang + '.xml').getText('UTF-8')
    def doc = DOMBuilder.newInstance().parseText(input)
    def encoding = doc.getXmlEncoding()
    if (!encoding.equalsIgnoreCase('UTF-8')) {
        println "Encoding is $encoding for $lang"
        input =  new File(lang + '.xml').getText(encoding)
        doc = DOMBuilder.newInstance().parseText(input)
    }
    def langpack = doc.documentElement
    use(DOMCategory) {
        return langpack.str
    }
}

private def getEnglishLangPack() {
    def input = new File('eng.xml').getText("UTF-8")
    def doc = DOMBuilder.newInstance().parseText(input)
    return doc.documentElement
}

private String findRefString(def refStrings, String id) {
    def node = refStrings.find({it.attributes['id'] == id})
    if (node) {
        return node.attributes['txt']
    }
    return null
}
