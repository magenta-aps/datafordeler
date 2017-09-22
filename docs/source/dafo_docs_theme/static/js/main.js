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

function jumpToLang(ev,lang) {
    ev.preventDefault();
    var urlPath = location.pathname.slice(4);
    urlPath = '/' + lang + '/' + urlPath;
    location.href = location.host + urlPath;
}

// When clicking language picker, redirect to same page with different translation
daLangLink.addEventListener("click", function(event){ jumpToLang(event,'da') });
klLangLink.addEventListener("click", function(event){ jumpToLang(event,'kl') });
enLangLink.addEventListener("click", function(event){ jumpToLang(event,'en') });
