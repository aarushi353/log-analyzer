const analyzeBtn = document.getElementById("analyzeBtn");
analyzeBtn.addEventListener("click", analyzeLogs);

async function analyzeLogs() {
    const logs = document.getElementById("logs").value;
    if (!logs.trim()) {
        showError("Please enter logs.");
        return;
    }

    hideError();
    document.getElementById("loading").classList.remove("hidden");
    document.getElementById("result").classList.add("hidden");

    try {

        const response = await fetch("https://log-analyzer-xcmd.onrender.com/api/logs/analyze", {
            method: "POST", headers: {
                "Content-Type": "application/json"
            }, body: JSON.stringify({
                logs: logs
            })
        });

        if (!response.ok) {
            throw new Error("API request failed");
        }

        const data = await response.json();
        renderResponse(data);

    } catch (error) {
        showError("Analysis failed. Please retry.");
    } finally {

        document
            .getElementById("loading")
            .classList
            .add("hidden");
    }
}

function renderResponse(data) {

    document.getElementById("summary").innerText = data.anomalySummary;
    renderList("rootCause", data.rootCause);
    renderList("recommendation", data.recommendation);

    document
        .getElementById("result")
        .classList
        .remove("hidden");
}

function renderList(elementId, items) {

    const list = document.getElementById(elementId);
    list.innerHTML = "";
    items.forEach(item => {
        const li = document.createElement("li");
        li.textContent = item;
        list.appendChild(li);
    });
}

function showError(message) {
    const errorDiv = document.getElementById("error");
    errorDiv.innerText = message;
    errorDiv.classList.remove("hidden");
}

function hideError() {
    document
        .getElementById("error")
        .classList
        .add("hidden");
}
