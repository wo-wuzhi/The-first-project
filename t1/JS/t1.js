document.querySelector("b").addEventListener("click", function(e) {
    alert("Clicked!");
});

const Myimage=document.querySelector("img");

Myimage.onclick=()=>{
    const Mysrc=Myimage.getAttribute("src");
    if(Mysrc==="images/2024051519361961631.jpg"){
        Myimage.setAttribute("src","images/2024051519373997073.jpg");
    }else{
        Myimage.setAttribute("src","images/2024051519361961631.jpg");
    }
};

let button=document.querySelector("button");
let c=document.querySelector("c");

function setname(){
    const name=prompt("Please enter your name.");
    if(!name){
        setname();
    }else{
        localStorage.setItem("name",name);
        c.textContent="宇宙最帅之人--"+name;
    }
}

if(!localStorage.getItem("name")){
    setname();
}else{
    c.textContent="宇宙最帅之人--"+localStorage.getItem("name");
}

button.onclick=()=>{
    setname();
};