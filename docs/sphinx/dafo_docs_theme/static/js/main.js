// Highlight selected language in language picker
var currentLangLink,
    daLangLink = document.querySelector(".lang-da"),
    klLangLink = document.querySelector(".lang-kl"),
    enLangLink = document.querySelector(".lang-en"),
    currentLangUrl = location.pathname.slice(0,4);

if (currentLangUrl === '/da/') {
    currentLangLink = daLangLink;
    currentLangLink.classList.add('active');
} else if (currentLangUrl === '/kl/') {
    currentLangLink = klLangLink;
    currentLangLink.classList.add('active');
} else if (currentLangUrl === '/en/') {
    currentLangLink = enLangLink;
    currentLangLink.classList.add('active');
}

// When clicking language picker, redirect to same page with different translation
function fixTranslationLink(el, language) {
    var locPath = location.pathname 
    var path = locPath.slice(4);
    if (locPath.slice(0,1) === '/' || path === '') {
        el.href = '/' + language + '/' + path; 
    }
}
fixTranslationLink(daLangLink, 'da');
fixTranslationLink(klLangLink, 'kl');
fixTranslationLink(enLangLink, 'en');
