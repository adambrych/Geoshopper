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

    var script_url = "../php/send.php";

    $.ajax({
        type: "POST",
        url: script_url,
        data: $("#registerForm").serialize(),
        success: function(response) {
            console.log("response: " + response);
            if(response == 1) {
                $('#msgSuccess').fadeIn('slow');
            } else {
                $('#msgError span').text("Dane niepoprawne. Spróbuj ponownie.");
                $('#msgError').fadeIn('slow');
            }
        },
        error: function(xhr, status, error) {
            console.log(xhr);
            console.log(status);
            console.log(error);
            $('#msgError span').text("Wystąpił nieoczekiwany błąd. Spróbuj ponownie.");
            $('#msgError').fadeIn('slow');
        }
    });
    return false;
}