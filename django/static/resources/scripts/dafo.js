var DAFO = window.DAFO || {};

(function($, w, DAFO) {


    // Close the dropdown if the user clicks outside of it
    w.onclick = function(event) {
        if (event.target.closest('.dropbtn') === null &&
            event.target.closest('.search-term') === null) {
            closeDropDowns();
        }
        if (event.target.id === "lightbox") {
            closePopups();
        }
    };


    w.closeDropDowns = function() {
        $(".dropdown-content").hide();
    };

    w.closePopups = function() {
        $("#lightbox").hide();
        $(".popup").hide();
    };

    w.onkeydown = function(evt) {
        evt = evt || window.event;
        // Escape
        if (evt.keyCode === 27) {
            closePopups();
        }
    };

    /* When the user clicks on the button,
    toggle between hiding and showing the dropdown content */
    w.toggleShow = function(id) {
        document.getElementById(id).classList.toggle("show");
    };

    w.showPopup = function(popupId) {
        $('#'+popupId).show();
        $("#lightbox").show();
    };

    w.show = function(id) {
        var element = document.getElementById(id);
        if (!element.classList.contains('show')) {
            element.classList.add('show');
        }
    };

    w.submitForm = function(inputId, action) {
        var input = document.getElementById(inputId);
        input.value = action;
    };


    w.newPassword = function() {
        var new_pw = Password.generate(16);
        document.getElementById("id_password").value = new_pw;
        document.getElementById("password_display").innerHTML = new_pw;
    };

    w.menuOpen = null;
    w.openMenu = function(item) {
        var menu = $(item);
        if (menu.length) {
            menu.show();
            if (w.menuOpen && !w.menuOpen.is(menu)) {
                w.menuOpen.hide();
            }
            w.menuOpen = menu;
        }
    };
    w.closeMenu = function(item) {
        var menu = $(item);
        if (menu.length) {
            menu.hide();
            w.menuOpen = null;
        }
    };
    w.toggleMenu = function(item) {
        var opened = w.menuOpen;
        if (w.menuOpen) {
            w.closeMenu(w.menuOpen);
        }
        if (!opened || !opened.is(item)) {
            w.openMenu(item);
        }
    };
    w.hoverMenu = function(item) {
        var menu = $(item);
        if (menu.length && w.menuOpen) {
            w.openMenu(menu);
        }
    };
    var Password = {

        _pattern : /[a-zA-Z0-9]/,

        _getRandomByte : function() {
            if(window.crypto && window.crypto.getRandomValues)
            {
                var result = new Uint8Array(1);
                window.crypto.getRandomValues(result);
                return result[0];
            }
            else if(window.msCrypto && window.msCrypto.getRandomValues)
            {
                var result = new Uint8Array(1);
                window.msCrypto.getRandomValues(result);
                return result[0];
            }
            else
            {
                return Math.floor(Math.random() * 256);
            }
        },

        generate : function(length) {
            return Array.apply(null, {'length': length})
                .map(function()
                {
                    var result;
                    while(true)
                    {
                        result = String.fromCharCode(this._getRandomByte());
                        if(this._pattern.test(result))
                        {
                            return result;
                        }
                    }
                }, this)
                .join('');
        }
    };

    var objectType;


    $(document).ready(function() {



        addEvent(window, 'load', function(e) {

            if ($('#id_certificates_to').length > 0 && $('#id_current_time').length > 0) {
                var currentTime = document.getElementById("id_current_time");
                var certificateElements = document.getElementById("id_certificates_to").children;
                for (var i = 0; i < certificateElements.length; ++i) {
                    if (certificateElements[i].title < currentTime.value)
                        certificateElements[i].style.color = "#FF0000";
                }
            }
            if ($('#id_certificates_add_link').length > 0) {
                var downloadElement = document.getElementById("id_certificates_add_link");
                downloadElement.id = "id_certificates_download";
            }

        });

        setOrderClasses();

        if ($('#object_type').length > 0) {

            // Initially set object type
            objectType = document.getElementById("object_type").value;

            $("#list_form").on("submit", function (event) {
                $.ajax({
                    url: "/ajax/update_" + objectType + "/",
                    type: "POST",
                    data: $('form').serialize(),
                    success: function (messageText) {
                        update_object().then(function (order) {
                            setOrderClasses();
                            var message = document.createElement("p");
                            message.innerHTML = messageText;
                            var messages = document.getElementById("messages");
                            while (messages.firstChild) {
                                messages.removeChild(messages.firstChild);
                            }
                            messages.appendChild(message);
                            messages.style.display = 'block';
                        });
                    }
                });
                event.preventDefault();
            });
        }

        $(".search-term").on("paste keyup focus", function () {
            var search_term = $(this).val();
            var id = this.id;

            var search_body = $('.' + id + '_body');

            search_body.html('').load(
                "/ajax/" + id + "/?search_term=" + encodeURIComponent(search_term)
            );
            if(search_body !== "") {
                closeDropDowns();
                search_body.addClass("show");
            }

        });

        $(".content")
            .on("click", ".ordering", function () {
                var element = $(this)[0].children[0];
                var id = element.id;
                var order = id.split("-")[1];
                toggle_order(order).then(function (isDescending) {
                    var addClass = isDescending ? "desc" : "asc";
                    var newElement = document.getElementById(id);
                    newElement.classList.add(addClass);
                });
            })
            .on("change", "#filter", function () {
                update_object().then(function (order) {
                    var id = "order-" + order.replace("-", "");
                    var isDescending = order.indexOf("-") !== -1;
                    var addClass = isDescending ? "desc" : "asc";
                    var newElement = document.getElementById(id);
                    newElement.classList.add(addClass);
                });
            })
            .on("click", "#checkbox_all", function () {
                var checkbox = $(this)[0];
                var inputs = document.getElementsByName("user_id");
                for (var i = 0; i < inputs.length; ++i) {
                    inputs[i].checked = checkbox.checked;
                }
            })
            .on("click", "#create_new_certificate", function () {
                toggleShow("new-certificate-box");
            })
            .on("click", "#id_certificates_download", function () {
                var certificates = document.getElementById("id_certificates_to");
                for (i=0; i<certificates.options.length; i++) {
                    var certElem = certificates.options[i];
                    if (certElem.selected) {
                        console.log(certElem.value);
                        window.location="certificate/" + certElem.value + "/download";
                    }
                }
            });

        function setOrderClasses(){
            var orderElement = document.getElementById("order");
            if(orderElement) {
                var order = orderElement.value;
                var elements = document.getElementsByClassName("ordering");
                for (var i = 0; i < elements.length; i++) {
                    var id = elements[i].children[0].id;
                    var elementOrder = id.split("-")[1];
                    if (order.indexOf(elementOrder) !== -1) {
                        var isDescending = order.indexOf("-") !== -1;
                        var addClass = isDescending ? "desc" : "asc";
                        var newElement = document.getElementById(id);
                        newElement.classList.add(addClass);
                    }
                }
            }
        }

        function update_object() {

            var orderKey = "order";
            var orderElement = document.getElementById(orderKey);
            var order = orderElement.value;

            var filterKey = "filter";
            var filterElement = document.getElementById(filterKey);
            var filter = filterElement.value;

            if(order !== "*")
                var query = orderKey + "=" + order;

            if(filter !== "*")
                query += "&" + filterKey + "=" + filter;

            return update_object_queryset(query).then(function(response){
                return order;
            });
        }

        function toggle_order(orderValue) {

            var orderKey = "order";
            var orderElement = document.getElementById(orderKey);
            var order = orderElement.value;

            var filterKey = "filter";
            var filterElement = document.getElementById(filterKey);
            var filter = filterElement.value;

            var isDescending = order === orderValue;
            var orderValue = (isDescending ? "-" : "") + orderValue;
            var query = orderKey + "=" + orderValue;

            if(filter !== "*")
                query += "&" + filterKey + "=" + filter;

            return update_object_queryset(query).then(function(response){
                orderElement.value = orderValue;
                return isDescending;
            });
        }

        function update_object_queryset(query) {
            return update_call("update_" + objectType + "_queryset", "?" + query);
        }

        function update_call(call, query) {
            return $.ajax({
                url: "/ajax/" + call + "/" + query,
                success: function(result){
                    var body = $('.' + call + "_body")[0];
                    body.innerHTML = result;
                }
            });
        }
    });

})(django.jQuery, window, DAFO);