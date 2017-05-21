$(document).ready(function () {
    $(document).ajaxStart(function () {
        $('#loading').css({'display': 'inline-block'});
        $('#buttonSend').attr('disabled', true);
    }).ajaxStop(function () {
        $('#loading').css({'display': 'none'});
        $('#buttonSend').attr('disabled', false);
    });
});

function sendForm() {

    var script_url = "/api/register";
    var data = {
        shopName: $("#shopName").val(),
        shopCity: $("#shopCity").val(),
        shopStreet: $("#shopStreet").val()
    };

    $.ajax({
        type: "POST",
        url: script_url,
        data: JSON.stringify(data),
        processData: false,
        contentType: "application/json; charset=UTF-8",
        success: function(response) {
            if(response.status == 1) {
                $('#msgSuccess').append("Twoj pin to " + response.pin + ".");
                $('#msgSuccess').fadeIn('slow');
            } else {
                $('#msgError span').text("Dane niepoprawne. Spróbuj ponownie.");
                $('#msgError').fadeIn('slow');
            }
        },
        error: function(xhr, status, error) {
            $('#msgError span').text("Wystąpił nieoczekiwany błąd. Spróbuj ponownie.");
            $('#msgError').fadeIn('slow');
        }
    });
    return false;
}