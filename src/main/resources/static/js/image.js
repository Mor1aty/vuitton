function showCoverImg(image) {
    document.getElementById("cover").style.display = "block";
    document.getElementById("cover-image").src = image.src;
}

function hideCoverImg() {
    document.getElementById("cover").style.display = "none";
}