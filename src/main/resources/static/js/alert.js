setTimeout(function () {
    var alerts = document.querySelectorAll(".alert");
    alerts.forEach(function (alert) {
      var bsAlert = new bootstrap.Alert(alert);
      bsAlert.close();
    });
  }, 3000);

  document.addEventListener("DOMContentLoaded", function () {
    var myCarousel = document.querySelector("#movieCarousel");
    var carousel = new bootstrap.Carousel(myCarousel, {
      interval: 3000,
      wrap: true,
    });
  });