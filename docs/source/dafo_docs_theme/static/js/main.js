// Highlight selected language in language picker
console.log(document.querySelector(".lang-da"));
var currentLangLink,
    daLangLink = document.querySelector(".lang-da"),
    klLangLink = document.querySelector(".lang-kl"),
    enLangLink = document.querySelector(".lang-en"),
    currentLangUrl = location.pathname.slice(0,4);
console.log('daLangLink');
console.log(daLangLink);
console.log('currentLangUrl');
console.log(currentLangUrl);
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
    console.log('location.pathname');
    console.log(location.pathname);
    var urlPath = location.pathname.slice(4);
    urlPath = '/' + lang + '/' + urlPath;
    console.log('Going to ' + location.host + location.urlPath);
    location.href = location.host + location.urlPath;
}

// When clicking language picker, redirect to same page with different translation
daLangLink.addEventListener("click", function(event){ jumpToLang(event,'da') });
//klLangLink.addEventListener("click", jumpToLang('kl'));
//enLangLink.addEventListener("click", jumpToLang('en'));
