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

// Create floating Sindhi letters in 3D space
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

function createFloatingLetters() {
  // Clear existing letters
  floatingLetters.innerHTML = ""

  // Create 3D floating letters
  for (let i = 0; i < 80; i++) {
    const letter = document.createElement("div")
    const randomLetter = sindhiLetters[Math.floor(Math.random() * sindhiLetters.length)]

    letter.className = "floating-letter"
    letter.textContent = randomLetter

    // Random 3D position
    const xPos = Math.random() * 100
    const yPos = Math.random() * 100
    const zPos = Math.random() * 500 - 250

    // Random size based on z-position (perspective effect)
    const size = Math.max(1, (zPos + 250) / 25)

    // Random rotation
    const rotX = Math.random() * 360
    const rotY = Math.random() * 360
    const rotZ = Math.random() * 360

    // Random animation duration and delay
    const duration = 10 + Math.random() * 20
    const delay = Math.random() * -20

    // Random color from Ajrak theme
    const colors = [
      "rgba(26, 105, 133, 0.7)", // ajrak-blue
      "rgba(214, 65, 97, 0.7)", // ajrak-red
      "rgba(139, 0, 0, 0.7)", // ajrak-maroon
      "rgba(233, 180, 76, 0.7)", // ajrak-gold
    ]
    const color = colors[Math.floor(Math.random() * colors.length)]

    letter.style.left = `${xPos}%`
    letter.style.top = `${yPos}%`
    letter.style.fontSize = `${size}rem`
    letter.style.color = color
    letter.style.textShadow = `0 0 3px ${color}`
  letter.style.transform = `translateZ(${zPos}px) rotateX(${rotX}deg) rotateY(${rotY}deg) rotateZ(${rotZ}deg)`
    letter.style.animationDuration = `${duration}s`
    letter.style.animationDelay = `${delay}s`

    floatingLetters.appendChild(letter)
  }
}

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
createFloatingLetters()
createPortrait()

// Recreate floating letters on window resize
window.addEventListener("resize", createFloatingLetters)

// Navigation Functions
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

          // 👉 Show floating letters only on welcomePage
          if (page === welcomePage) {
            floatingLetters.style.display = "block"
            createFloatingLetters() // Optional: recreate for fresh look
          } else {
            floatingLetters.style.display = "none"
          }

        }, 50)
      }, 400)
    }
  })

  // If no active page (first load)
  if (!document.querySelector(".page.active")) {
    page.classList.add("active")
    setTimeout(() => {
      page.style.opacity = "1"
      page.style.transform = "translateY(0)"

      // 👉 Initial load case
      if (page === welcomePage) {
        floatingLetters.style.display = "block"
      } else {
        floatingLetters.style.display = "none"
      }
    }, 50)
  }
}



// Add 3D effect to buttons
function add3DEffectToButtons() {
  const buttons = document.querySelectorAll(".btn")

  buttons.forEach((btn) => {
    btn.addEventListener("mousemove", (e) => {
      const rect = btn.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      const centerX = rect.width / 2
      const centerY = rect.height / 2

      const angleX = (y - centerY) / 10
      const angleY = (centerX - x) / 10

      btn.style.transform = `perspective(500px) rotateX(${angleX}deg) rotateY(${angleY}deg) translateZ(10px)`
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

// Simple interpreter function (simulation)
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
          } else if (varNameWithType.includes("لڱا")) {
            varType = "string"
            varName = varNameWithType.replace("لڱا", "").trim()
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
          } else if (varNameWithType.includes("لڱا")) {
            const varName = varNameWithType.replace("لڱا", "").trim()
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
