// DOM Elements
const welcomePage = document.getElementById("welcome-page")
const editorPage = document.getElementById("editor-page")
const docsPage = document.getElementById("docs-page")
const startBtn = document.getElementById("start-btn")
const docsBtn = document.getElementById("docs-btn")
const backFromEditorBtn = document.getElementById("back-from-editor")
const backFromDocsBtn = document.getElementById("back-from-docs")
const runBtn = document.getElementById("run-btn")
const resetBtn = document.getElementById("reset-btn")
const codeEditor = document.getElementById("code-editor")
const outputConsole = document.getElementById("output-console")
const floatingLetters = document.querySelector(".floating-letters")

// Create floating Sindhi letters in 3D space - OPTIMIZED
const sindhiLetters = [
  "ا",
  "ب",
  "ٻ",
  "ڀ",
  "پ",
  "ت",
  "ٿ",
  "ٽ",
  "ث",
  "ج",
  "جھ",
  "ڄ",
  "چ",
  "ڇ",
  "ح",
  "خ",
  "د",
  "ڌ",
  "ڏ",
  "ذ",
  "ر",
  "ڙ",
  "ز",
  "س",
  "ش",
  "ص",
  "ض",
  "ط",
  "ظ",
  "ع",
  "غ",
  "ف",
  "ڦ",
  "ق",
  "ڪ",
  "ک",
  "گ",
  "ڳ",
  "ڱ",
  "ل",
  "م",
  "ن",
  "ڻ",
  "و",
  "ھ",
  "ء",
  "ي",
]

// Throttle function to limit how often a function can be called
function throttle(func, limit) {
  let inThrottle
  return function () {
    const args = arguments
    
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

// Optimized floating letters creation - reduced count and complexity
function createFloatingLetters() {
  floatingLetters.innerHTML = ""

  const isMobile = window.innerWidth <= 768
  const letterCount = isMobile ? 10 : 20 // Safer for low-end devices

  const fragment = document.createDocumentFragment()

  for (let i = 0; i < letterCount; i++) {
    const letter = document.createElement("div")
    const randomLetter = sindhiLetters[Math.floor(Math.random() * sindhiLetters.length)]

    letter.className = "floating-letter"
    letter.textContent = randomLetter

    const xPos = Math.random() * 100
    const yPos = Math.random() * 100
    const zPos = isMobile ? 0 : (Math.random() * 300 - 150) // Disable z transform on mobile

    const size = isMobile
      ? Math.random() * 1.5 + 1.2 // Smaller sizes on mobile
      : Math.max(1, (zPos + 150) / 25)

    const duration = 10 + Math.random() * 5
    const delay = Math.random() * -5

    const colors = [
      "rgba(26, 105, 133, 0.7)",
      "rgba(214, 65, 97, 0.7)",
      "rgba(139, 0, 0, 0.7)",
      "rgba(233, 180, 76, 0.7)",
    ]
    const color = colors[Math.floor(Math.random() * colors.length)]

    letter.style.left = `${xPos}%`
    letter.style.top = `${yPos}%`
    letter.style.fontSize = `${size}rem`
    letter.style.color = color
    letter.style.transform = isMobile ? "none" : `translateZ(${zPos}px)`
    letter.style.animationDuration = `${duration}s`
    letter.style.animationDelay = `${delay}s`

    fragment.appendChild(letter)
  }

  floatingLetters.appendChild(fragment)
}


// Throttled version of createFloatingLetters to prevent performance issues
const throttledCreateFloatingLetters = throttle(createFloatingLetters, 1000)

// Initialize floating letters
createFloatingLetters()

// Recreate floating letters on window resize, but throttled
window.addEventListener("resize", throttledCreateFloatingLetters)

// Create portrait with Ajrak pattern
function createPortrait() {
  const portraitImg = document.querySelector(".portrait-img")
  if (portraitImg) {
    portraitImg.style.background = `
      linear-gradient(45deg, rgba(26, 105, 133, 0.8), rgba(139, 0, 0, 0.8)),
      repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(233, 180, 76, 0.3) 10px, rgba(233, 180, 76, 0.3) 20px),
      repeating-linear-gradient(-45deg, transparent, transparent 10px, rgba(214, 65, 97, 0.3) 10px, rgba(214, 65, 97, 0.3) 20px)
    `
  }
}

// Initialize floating letters and portrait
createPortrait()

// Recreate floating letters on window resize
// Navigation Functions - Optimized
function showPage(page) {
  // Hide all pages with animation
  document.querySelectorAll(".page").forEach((p) => {
    if (p.classList.contains("active")) {
      p.style.opacity = "0"
      p.style.transform = "translateY(20px)"

      setTimeout(() => {
        p.classList.remove("active")

        // Show selected page with animation
        page.classList.add("active")
        setTimeout(() => {
          page.style.opacity = "1"
          page.style.transform = "translateY(0)"

          // Show floating letters only on welcomePage
          if (page === welcomePage) {
            floatingLetters.style.display = "block"
            // Only recreate if they don't exist
            if (floatingLetters.children.length === 0) {
              createFloatingLetters()
            }
          } else {
            floatingLetters.style.display = "none"
          }
        }, 50)
      }, 300) // Reduced from 400ms
    }
  })

  // If no active page (first load)
  if (!document.querySelector(".page.active")) {
    page.classList.add("active")
    setTimeout(() => {
      page.style.opacity = "1"
      page.style.transform = "translateY(0)"

      // Initial load case
      if (page === welcomePage) {
        floatingLetters.style.display = "block"
      } else {
        floatingLetters.style.display = "none"
      }
    }, 50)
  }
}

// Simplified 3D effect for buttons - using CSS transforms instead of JS
function add3DEffectToButtons() {
  const buttons = document.querySelectorAll(".btn")

  buttons.forEach((btn) => {
    btn.addEventListener("mousemove", (e) => {
      const rect = btn.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      const centerX = rect.width / 2
      const centerY = rect.height / 2

      // Reduced angle for less extreme effect
      const angleX = (y - centerY) / 15
      const angleY = (centerX - x) / 15

      btn.style.transform = `perspective(500px) rotateX(${angleX}deg) rotateY(${angleY}deg) translateZ(5px)`
    })

    btn.addEventListener("mouseleave", () => {
      btn.style.transform = ""
    })
  })
}

// Event Listeners
startBtn.addEventListener("click", () => {
  showPage(editorPage)
})

docsBtn.addEventListener("click", () => {
  showPage(docsPage)
})

backFromEditorBtn.addEventListener("click", () => {
  showPage(welcomePage)
})

backFromDocsBtn.addEventListener("click", () => {
  showPage(welcomePage)
})

// Editor Functionality
runBtn.addEventListener("click", () => {
  const code = codeEditor.value

  if (!code.trim()) {
    outputConsole.innerHTML = '<span style="color: #d64161;">ڪوڊ خالي آهي. ڪجھ لکو!</span>'
    return
  }

  try {
    // Simple interpreter simulation
    const output = interpretCode(code)
    outputConsole.innerHTML = output
  } catch (error) {
    outputConsole.innerHTML = `<span style="color: #d64161;">خرابي: ${error.message}</span>`
  }
})

resetBtn.addEventListener("click", () => {
  codeEditor.value = ""
  outputConsole.innerHTML = ""
})

// Simple interpreter function (simulation) - unchanged
function interpretCode(code) {
  let output = ""
  const lines = code.split("\n")
  const variables = {}

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()

    if (!line) continue

    // Variable declaration
    if (line.startsWith("لک")) {
      try {
        const varDeclaration = line.substring(3).trim()
        const parts = varDeclaration.split("=")

        if (parts.length === 2) {
          const varNameWithType = parts[0].trim()
          const varValue = parts[1].trim()

          let varName, varType

          if (varNameWithType.includes("عددي")) {
            varType = "number"
            varName = varNameWithType.replace("عددي", "").trim()
          } else if (varNameWithType.includes("لکت")) {
            varType = "string"
            varName = varNameWithType.replace("لکت", "").trim()
          } else {
            varName = varNameWithType
            // Infer type
            if (varValue.startsWith('"') && varValue.endsWith('"')) {
              varType = "string"
            } else {
              varType = "number"
            }
          }

          // Process value based on type
          if (varType === "number") {
            variables[varName] = Number(varValue)
          } else {
            // Remove quotes for strings
            variables[varName] = varValue.replace(/^"|"$/g, "")
          }
        } else if (parts.length === 1) {
          // Just declaring without assigning
          const varNameWithType = parts[0].trim()

          if (varNameWithType.includes("عددي")) {
            const varName = varNameWithType.replace("عددي", "").trim()
            variables[varName] = 0
          } else if (varNameWithType.includes("لکت")) {
            const varName = varNameWithType.replace("لکت", "").trim()
            variables[varName] = ""
          }
        }
      } catch (e) {
        output += `<span style="color: #d64161;">خرابي: ${i + 1} لائن تي ڦرڻي جي اعلان ۾ مسئلو</span><br>`
      }
    }
    // Output command
    else if (line.startsWith("لکيوَ")) {
      try {
        const outputValue = line.substring(5).trim()

        // Check if it's a variable
        if (variables.hasOwnProperty(outputValue)) {
          output += `${variables[outputValue]}<br>`
        } else {
          // It's a string literal
          output += `${outputValue.replace(/^"|"$/g, "")}<br>`
        }
      } catch (e) {
        output += `<span style="color: #d64161;">خرابي: ${i + 1} لائن تي آؤٽ پٽ ۾ مسئلو</span><br>`
      }
    }
    // Simple variable assignment (without لک)
    else if (line.includes("=")) {
      try {
        const parts = line.split("=")
        if (parts.length === 2) {
          const varName = parts[0].trim()
          const varValue = parts[1].trim()

          if (variables.hasOwnProperty(varName)) {
            // Update existing variable
            if (typeof variables[varName] === "number") {
              // Handle simple arithmetic
              if (varValue.includes("+")) {
                const addParts = varValue.split("+")
                const left = addParts[0].trim()
                const right = addParts[1].trim()

                const leftValue = variables.hasOwnProperty(left) ? variables[left] : Number(left)
                const rightValue = variables.hasOwnProperty(right) ? variables[right] : Number(right)

                variables[varName] = leftValue + rightValue
              } else {
                variables[varName] = Number(varValue)
              }
            } else {
              // String
              variables[varName] = varValue.replace(/^"|"$/g, "")
            }
          }
        }
      } catch (e) {
        output += `<span style="color: #d64161;">خرابي: ${i + 1} لائن تي قيمت مقرر ڪرڻ ۾ مسئلو</span><br>`
      }
    }
  }

  return output || '<span style="color: #e9b44c;">ڪو نتيجو ناهي</span>'
}

// Add 3D effects to buttons
add3DEffectToButtons()

// Initialize the app
showPage(welcomePage)

// Add event listener for page visibility to pause animations when tab is not visible
document.addEventListener("visibilitychange", () => {
  if (document.hidden) {
    // Page is hidden, pause intensive animations
    document.querySelectorAll(".floating-letter").forEach((letter) => {
      letter.style.animationPlayState = "paused"
    })
  } else {
    // Page is visible again, resume animations
    document.querySelectorAll(".floating-letter").forEach((letter) => {
      letter.style.animationPlayState = "running"
    })
  }
})

// Use requestAnimationFrame for smoother animations
let lastTime = 0
function animateLetters(timestamp) {
  if (!lastTime) lastTime = timestamp
  const elapsed = timestamp - lastTime

  // Only update if enough time has passed (60fps = ~16ms)
  if (elapsed > 16) {
    lastTime = timestamp

    // Only animate if welcome page is active and visible
    if (welcomePage.classList.contains("active") && !document.hidden) {
      document.querySelectorAll(".floating-letter").forEach((letter) => {
        // Get current transform
        const transform = letter.style.transform
        // Extract Z value
        const zMatch = transform.match(/translateZ$$([^)]+)$$/)
        if (zMatch && zMatch[1]) {
          const currentZ = Number.parseFloat(zMatch[1])
          // Slowly update Z position for subtle movement
          const newZ = currentZ + Math.sin(timestamp / 1000) * 0.5
          letter.style.transform = transform.replace(/translateZ$$[^)]+$$/, `translateZ(${newZ}px)`)
        }
      })
    }
  }

  requestAnimationFrame(animateLetters)
}

document.getElementById("run-btn").addEventListener("click", async () => {
  const textarea = document.getElementById("code-editor");
  const resultDiv = document.getElementById("output-console");
  const runBtn = document.getElementById("run-btn");
  const apiUrl = "http://localhost:8080/SindhiLanguage/v1";

  const userCode = textarea.value.trim();
  if (!userCode) {
    resultDiv.innerText = "مهرباني ڪري ڪوڊ لکو.";
    return;
  }

  const payload = { sindhiCode: userCode };

  runBtn.disabled = true;
  runBtn.innerText = "⏳ لوڊ ٿي رهيو آهي...";

  try {
    const response = await fetch(apiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`);
    }

    const resultText = await response.text();
    resultDiv.innerText = resultText;
    resultDiv.scrollIntoView({ behavior: "smooth", block: "start" });

  } catch (error) {
    resultDiv.innerText = "غلطي: " + error.message;
    console.error("API Error:", error);
  } finally {
    runBtn.disabled = false;
    runBtn.innerText = "هلايو";
  }
});
const latifQuotes = [
    "جي سڄڻ سان نيهه لڳو، تن جو ڪجھ به نه وڃي،<br>جيئن سمنڊ ۾ وهي وڃن، سدائين موتي ٿين",
    "ٻوليءَ ۾ برڪت آهي، ڀلي پاڻ گهڻو نه ڳالهاءِ",
    "سادن جي سچ ڳالهه، سونهاري سڌ آڻي",
    "جيڪو سچيءَ ريت سان ڳالهايو، تنهن جو نالو هميشه زنده",
    "دل جي ڳالهه لکي وڃ، تن کي عقل اچي",
    "حڪمت ڳالهيون کوجن، سي ڀانئجن ڪوڙ نه",
    "ڪلام ۾ علم آهي، علم ۾ نجات آهي",
    "لکڻ لکائيءَ سان دلين کي سڪون ملي",
    "جتي بولي پنهنجي، اُتي عزت ٻيڻي",
    "دل جي اک کلي، ته سچ جي سُڌ پوي",
    "لفظن جو لوءُ جلاءِ، اُهو ئي علم وارو ٿئي"
  ];

  let currentIndex = Math.floor(Math.random() * 10);
  const verseElement = document.querySelector(".verse");

  function showQuoteWithFade() {
    // Fade out
    verseElement.style.opacity = 0;

    setTimeout(() => {
      // Change quote after fade out
      verseElement.innerHTML = latifQuotes[currentIndex];
      verseElement.style.opacity = 1; // Fade in

      // Move to next
      currentIndex = (currentIndex + 1) % latifQuotes.length;
    }, 1000); // Match CSS transition duration (1s)
  }

  // Initial display
  showQuoteWithFade();

  // Every 2 minutes
  setInterval(showQuoteWithFade, 100000);
// Start the animation loop
requestAnimationFrame(animateLetters)
