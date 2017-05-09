$('form').validate({

    showErrors: function(errorMap, errorList) {

        $.each(this.validElements(), function (index, element) {
            var $element = $(element);
            $element.data("title", "")
                .removeClass("error")
                .tooltip("destroy");
        });

        $.each(errorList, function (index, error) {
            var $element = $(error.element);
            $element.tooltip("destroy")
                .data("title", error.message)
                .addClass("error")
                .tooltip();
        });
    },
    submitHandler: function(form) {
        sendForm();
    }
});