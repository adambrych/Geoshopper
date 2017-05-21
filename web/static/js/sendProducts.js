var fileContent;

$(document).ready(function () {
    $(document).ajaxStart(function () {
        $('#loading').css({'display': 'inline-block'});
        $('#buttonSend').attr('disabled', true);
    }).ajaxStop(function () {
        $('#loading').css({'display': 'none'});
        $('#buttonSend').attr('disabled', false);
    });
});

function readSingleFile(e) {
    console.log("witanko");
    $('#loading').css({'display': 'inline-block'});
    $('#buttonSend').attr('disabled', true);
    var file = e.target.files[0];
    if (!file) {
        return;
    }
    var reader = new FileReader();
    reader.onload = function(e) {
        fileContent = e.target.result;
        console.log(fileContent);
        $('#loading').css({'display': 'none'});
        $('#buttonSend').attr('disabled', false);
    };
    reader.readAsText(file);
}

document.getElementById('shopProducts')
    .addEventListener('change', readSingleFile, false);

function sendForm() {

    var script_url = "/api/products";
    var data = {
        shopName: $("#shopName").val(),
        shopPin: $("#shopPin").val(),
        shopPromotionFrom: $("#shopPromotionFrom").val(),
        shopPromotionTo: $("#shopPromotionTo").val(),
        shopProducts: fileContent
    };

    $.ajax({
        type: "POST",
        url: script_url,
        data: JSON.stringify(data),
        processData: false,
        contentType: "application/json; charset=UTF-8",
        success: function(response) {
            if(response.status == 1) {
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